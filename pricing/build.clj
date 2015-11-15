(require 'cljs.build.api)

(cljs.build.api/build "src"
  {:main 'pricing.core
   :output-to "main.js"
   :target :nodejs})
