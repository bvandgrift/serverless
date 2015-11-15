(ns preso.aws)

(def AWS js/AWS)
(def aws-region "us-east-1")
(def aws-credentials (AWS.CognitoIdentityCredentials. 
                       #js {:IdentityPoolId 
                            "us-east-1:13d149ac-036d-4108-a33e-ef4b6357ccfd"}))

(defn aws-config-cognito! []
  (set! (.. AWS -config -region) aws-region)
  (set! (.. AWS -credentials) aws-credentials))
