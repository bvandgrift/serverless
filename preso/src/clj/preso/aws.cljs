(ns preso.aws)

(def AWS js/AWS)
(def region "us-east-1")

(defn credentials [user]
  (new AWS.CognitoIdentityCredentials
    (clj->js
      {:IdentityPoolId 
       "us-east-1:13d149ac-036d-4108-a33e-ef4b6357ccfd"
       :IdentityId (get user "IdentityId")
       :Logins {"cognito-identity.amazonaws.com" (get user "Token")}})))

(defn config-cognito! [token]
  (set! (.. AWS -config -region) region)
  (set! (.. AWS -config -credentials) (credentials token)))
