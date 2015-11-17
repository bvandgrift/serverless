(ns preso.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]))

(def loading-page (slurp "resources/public/index.html"))

(defroutes routes
  (GET "/"        [] loading-page)
  (GET "/pricing" [] loading-page)
  (GET "/about"   [] loading-page)
  (GET "/sign-in" [] loading-page)
  (GET "/sign-up" [] loading-page)
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
