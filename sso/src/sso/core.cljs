(ns sso.core
  (:require [cljs-lambda.util :refer [async-lambda-fn
                                      wrap-lambda-fn
                                      succeed! fail!]]
            [cljs.nodejs :as node])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(node/enable-util-print!)

(def bcrypt (node/require "bcrypt-nodejs"))
(defn- hash-password [pw] 
  (let [salt (.genSaltSync bcrypt)]
    (.hashSync bcrypt pw salt)))

(def AWS (node/require "aws-sdk"))
(def aws-region "us-east-1")
(def aws-identity-pool-id "us-east-1:13d149ac-036d-4108-a33e-ef4b6357ccfd")

(set! (.. AWS -config -region) aws-region)

(def dynamo-db (AWS.DynamoDB.))
(def dynamo-table-name "serverless-sso")

(def cognito-identity (AWS.CognitoIdentity.))

(defn get-user [email success]
  (let [params {:Key {:email {:S email}}
                :TableName dynamo-table-name}]
    (.getItem dynamo-db (clj->js params) 
              #(if %1 (println "error" %1)
                 (let [user  (js->clj %2 :keywordize-keys true)
                       email (get-in user [:Item :email :S])
                       pass  (get-in user [:Item :password :S])
                       cogid (get-in user [:Item :cognitoId :S])
                       out   (if (empty? user) {}
                               {:email email
                                :password pass
                                :cognitoId cogid})]
                   (success out))))))

(defn get-cognito-id [user success]
  (let [params {:IdentityPoolId aws-identity-pool-id
                :Logins {"serverless.aerost.at" (:email user)}
                :IdentityId (:cognitoId user)}]
    (.getOpenIdTokenForDeveloperIdentity 
      cognito-identity (clj->js params)
      #(if %1 (println "error" %1)
         (success (js->clj %2 :keywordize-keys true))))))

(defn make-user [user success]
  (get-cognito-id 
    user
    (fn [{:keys [IdentityId]}]
      (let [email    (:email user)
            password (hash-password (:password user))
            params   {:Item {:email     {:S email}
                             :password  {:S password}
                             :cognitoId {:S IdentityId}}
                      :TableName dynamo-table-name}]
        (.putItem dynamo-db (clj->js params) 
                  #(if %1 (println "error" %1)
                     (success {:email email})))))))

(defn sign-in-user [session user success]
  (println "session" session "user" user)
  (if (.compareSync bcrypt (:password session) (:password user))
    (get-cognito-id user success)
    (success {:error "password does not match"})))

(def ^:export create-user
  (wrap-lambda-fn
    (fn [{:keys [user]} context]
      (get-user (:email user) 
                #(if (empty? %)
                   (make-user user (partial succeed! context))
                   (succeed! context
                             {:error (str "user " (:email %) " already exists")}))))))

(def ^:export send-email
  (async-lambda-fn
    (fn [event context])))

(def ^:export create-session
  (wrap-lambda-fn
    (fn [{:keys [session]} context]
      (get-user (:email session)
                #(if (empty? %)
                   (succeed! context {:error "user not found"})
                   (sign-in-user session % (partial succeed! context)))))))

(defn -main [& args]
  (println "running")
  )
  ;; (create-session #js {:session {:email    (first args)
  ;;                                :password (last args)}}
  ;;                 #js {:succeed #(println "success" %)
  ;;                      :fail    #(println "failure" %)}))
  ;; (create-user #js {:user {:email 
  ;;                          (str "adam" (first args) "@aerost.at")
  ;;                          :password "kittykat"}}
  ;;              #js {:succeed #(println "success" %)
  ;;                   :fail    #(println "failure" %)}))

(set! *main-cli-fn* -main)
