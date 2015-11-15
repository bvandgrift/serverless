(defproject sso "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "http://please.FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.2.371"]
                 [io.nervous/cljs-lambda "0.1.2"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-npm "0.5.0"]
            [io.nervous/lein-cljs-lambda "0.2.4"]]
  :node-dependencies [[source-map-support "0.2.8"]
                      [aws-sdk "2.2.17"]]
  :source-paths ["src"]
  :cljs-lambda
  {:defaults {:role "arn:aws:iam::472521722016:role/serverless-sso"}
   :aws-profile "aerostat"
   :functions
   [{:name   "work-magic"
     :invoke sso.core/work-magic}
    {:name   "create-user"
     :invoke sso.core/create-user
     :description "create a user in dynamo for cognito"
     :memory-size 1024}
    {:name   "create-session"
     :invoke sso.core/create-session
     :description "authenticate a user in dynamo for cognito"
     :memory-size 1024}]}
  :cljsbuild
  {:builds [{:id "sso"
             :source-paths ["src"]
             :compiler {:output-to "out/sso.js"
                        :output-dir "out"
                        :target :nodejs
                        :main sso.core
                        :optimizations :none
                        :source-map true}}]}
 :aliases {"noderepl" ["run" "-m" "clojure.main" "repl.clj"]})
