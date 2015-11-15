(ns preso.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET]]))

;; ------------------------
;; Endpoints
(def api-pricing-lambda "https://7w1dsa6ira.execute-api.us-east-1.amazonaws.com/staging/pricing/lambda")

;; -------------------------
;; Views

(defn home-page []
  (println "home page!")
  (GET api-pricing-lambda)
  [:div [:h2 "Welcome to preso"]
   [:div [:a {:href "/about"} "go to about page"]]])

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
