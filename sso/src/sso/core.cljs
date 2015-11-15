(ns sso.core
  (:require [cljs-lambda.util :refer [async-lambda-fn wrap-lambda-fn]]
            [cljs.nodejs :as node])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(node/enable-util-print!)

(def AWS (node/require "aws-sdk"))
(def aws-region "us-east-1")

(set! (.. AWS -config -region) aws-region)

(def ^:export work-magic
  (async-lambda-fn
   (fn [{:keys [variety]} context]
     (go
       (js/Error (str "Sorry, I don't yet know how to work: '" variety "' magic"))))))

(def dynamo-db (AWS.DynamoDB.))
(def dynamo-table-name "serverless-sso")

(defn get-user [email success-fn failure-fn]
  (let [params {:Key {:email {:S email}}
                :TableName dynamo-table-name}]
    (.getItem dynamo-db (clj->js params) 
              #(if %1 failure-fn success-fn))))

(defn clj-create-user [{:keys [user]} context]
  (get-user (:email user) #(println "error" %) #(println "record" %))
  (println "Not much to do yet for creating users")
  user)

(def ^:export create-user
  (wrap-lambda-fn clj-create-user))

(def ^:export create-session
  (async-lambda-fn
   (fn [event context]
     (go
       (println "not much to do yet for creating sessions")))))

(defn -main [& args]
  (println "running"
           (create-user #js {:user {:email "adam@aerost.at"}}
                        #js {:succeed #(println "success" %)})))

(set! *main-cli-fn* -main)
