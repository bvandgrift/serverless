(ns pricing.core
  (:require [cljs.nodejs :as node]))

(node/enable-util-print!)

(def request (node/require "request"))
(def cheerio (node/require "cheerio"))

(def url "https://aws.amazon.com/lambda/pricing/")

(def errors {:error "pricing not available"})

(defn format-prices [js-prices]
  (let [prices  (partition 3 (vec js-prices))
        headers (first prices)
        values  (rest prices)
        zipped  (map #(map vector headers %) values)]
    (map #(into {} %) zipped)))

(defn parse-prices [body]
  (let [$    (.load cheerio body)
        sel  ".aws-table table tbody tr td"
        dom  ($ sel)
        text #(.text ($ %2))]
    (format-prices (.get (.map dom text)))))

(defn request-prices [success failure]
  (request url (fn [error response body]
           (if-not error 
             (success (parse-prices body)) (failure errors)))))

(defn handler [event context]
  (request-prices #(.succeed context (clj->js %))
                  #(.fail    context (clj->js %))))

(defn -main [& args]
  (println "Booting up clojurescript/node")
  (handler nil #js {:succeed #(println "success" %)
                    :fail    #(println "error" %)}))

(set! *main-cli-fn* -main)
(set! (.-exports js/module) #js {:handler handler})
