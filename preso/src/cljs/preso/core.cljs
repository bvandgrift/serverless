(ns preso.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              ;; [preso.aws :as AWS]
              [ajax.core :refer [GET]]))

;; ------------------------
;; Endpoints
(def api-pricing-lambda-url "https://7w1dsa6ira.execute-api.us-east-1.amazonaws.com/staging/pricing/lambda")
(def api-pricing-ec2-url "/data/ec2.json")

;; AWS.config.region = 'us-east-1'; // Region
;; AWS.config.credentials = new AWS.CognitoIdentityCredentials({
;;     IdentityPoolId: 'us-east-1:13d149ac-036d-4108-a33e-ef4b6357ccfd',
;; });

;; state
(def app-state (reagent/atom {:lambda {} :ec2 {}}))

;; -------------------------
;; Views

(defn home-page []
  (println "home page!")
  ;; (println AWS)
  ;; (aws-config-cognito!)
  [:div [:h2 "Serverless Microservices"]
   [:ul 
    [:li [:a {:href "/pricing"} "compare lambda & ec2 pricing &raquo;"]]
    [:li [:a {:href "/about"} "about this presentation"]]]])

(defn get-api-pricing-lambda []
  (GET api-pricing-lambda-url :handler #(swap! app-state assoc :lambda %)))

(defn get-api-pricing-ec2 []
  (GET api-pricing-ec2-url :handler #(swap! app-state assoc :ec2 %)))

(defn pricing-lambda []
  [:ul
   (for [x (:lambda @app-state)]
     [:li {:key (get x "Memory (MB)")}
      [:pre (JSON.stringify x)]])])

(defn pricing-ec2 []
  [:ul
   (for [x (:ec2 @app-state)]
     [:li {:key (get x "greeting")}
      [:pre (JSON.stringify x)]])])

(defn pricing []
  [:div 
   [:h2 "Compare AWS pricing"]
   [:h3 "lambda vs ec2"]
   (pricing-lambda)
   (pricing-ec2)])
  
(defn pricing-page []
  (get-api-pricing-lambda)
  (get-api-pricing-ec2)
  pricing)

(defn about-page []
  [:div [:h2 "About preso"]
   [:div [:a {:href "/"} "go to the home page"]]])

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

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
