(ns preso.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [clojure.string :as string]
              [preso.aws :as AWS]
              [ajax.core :refer [GET POST]]))

;; ------------------------
;; Endpoints
(def api-host "https://7w1dsa6ira.execute-api.us-east-1.amazonaws.com/staging")
(defn- prefix-api-host [path] (str api-host path))
(def api-pricing-lambda-url (prefix-api-host "/pricing/lambda"))
(def api-pricing-ec2-url    (prefix-api-host "/pricing/ec2"))
(def api-sign-in-url        (prefix-api-host "/session"))
(def api-sign-up-url        (prefix-api-host "/user"))

;; AWS.config.region = 'us-east-1'; // Region
;; AWS.config.credentials = new AWS.CognitoIdentityCredentials({
;;     IdentityPoolId: 'us-east-1:13d149ac-036d-4108-a33e-ef4b6357ccfd',
;; });

;; state
(def app-state (reagent/atom {:lambda [] :ec2 [] :inputs {}}))

;; -------------------------
;; Views

(defn home-page []
  (println "home page!")
  [:div [:h2 "Serverless Microservices"]
   [:ul 
    [:li [:a {:href "/pricing"} "compare lambda & ec2 pricing"]]
    [:li [:a {:href "/about"} "about this presentation"]]]])

(defn get-api-pricing-lambda []
  (GET api-pricing-lambda-url :handler #(swap! app-state assoc :lambda %)))

(defn parse-aws-ec2 [text]
  (let [parts  (string/split text "callback")
        good   (last parts)]
    (get-in (js->clj (js/eval good) :keywordize-keys true) [:config])))

(defn get-api-pricing-ec2 []
  (GET api-pricing-ec2-url :handler #(swap! app-state assoc :ec2 (parse-aws-ec2 %)) :response-format :text))

(defn get-value [id]
  (get-in @app-state [:inputs id]))

(defn set-value! [id value]
  (swap! app-state assoc-in [:inputs id] value))

(defn pricing-lambda []
  [:ul
   (for [x (:lambda @app-state)]
     [:li {:key (get x "Memory (MB)")}
      [:pre (JSON.stringify (clj->js x))]])])

(defn pricing-ec2 []
  [:ul
   (for [x (:ec2 @app-state)]
     [:li {:key (get x "greeting")}
      [:pre (JSON.stringify x)]])])

(defn pricing []
  [:div 
   [:h2 "Compare AWS pricing"]
   [:h3 "lambda vs ec2"]
   (pricing-lambda)])
  
(defn pricing-page []
  (get-api-pricing-lambda)
  (get-api-pricing-ec2)
  pricing)

(defn about-page []
  [:div [:h2 "About preso"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn- email-input []
  [:p
   [:label {:for "email"}
    "Email"]
   [:input {:type "text"
            :id "email"
            :value (get-value :email)
            :on-change #(set-value! :email (.. % -target -value))}]])

(defn- password-input []
  [:p
   [:label {:for "password"}
    "Password"]
   [:input {:type "text"
            :id "password"
            :value (get-value :password)
            :on-change #(set-value! :password (.. % -target -value))}]])

(defn do-sign-up []
  (POST api-sign-up-url
        :format :json
        :handler #(swap! app-state assoc :user %)
        :params {:user {:email (get-value :email)
                        :password (get-value :password)}}))

(defn do-sign-in []
  (POST api-sign-in-url
        :format :json
        :handler #(do (swap! app-state assoc :user %)
                      (AWS/config-cognito! (get @app-state :user)))
        :params {:session {:email (get-value :email)
                           :password (get-value :password)}}))

(defn sign-up-form []
   [:div 
    (email-input)
    (password-input)
    [:button {:on-click do-sign-up} "Sign Up!"]])

(defn sign-in-form []
  [:div
   (email-input)
   (password-input)
   [:button {:on-click do-sign-in} "Sign In!"]])

(defn sign-in-link []
  [:a {:href "/sign-in"} "Sign In :)"])

(defn show-user []
  [:div [:p (get-in @app-state [:user "email"])]])

(defn show-token []
  [:div [:p (get-in @app-state [:user "Token"])]])

(defn sign-up-page []
  [:div [:h2 "Sign up"]
   (if (:user @app-state) [:div (show-user) (sign-in-link)] (sign-up-form))])

(defn sign-in-page []
  [:div [:h2 "Sign in"]
   (if (:user @app-state) (show-token) (sign-in-form))])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
;; (secretary/set-config! :prefix  "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/pricing" []
  (session/put! :current-page #'pricing-page)) 

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page)) 

(secretary/defroute "/sign-up" []
  (session/put! :current-page #'sign-up-page)) 

(secretary/defroute "/sign-in" []
  (session/put! :current-page #'sign-in-page)) 
;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
