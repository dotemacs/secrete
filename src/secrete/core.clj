(ns secrete.core
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [cognitect.aws.client.api :as aws]
            [malli.core :as m]))


(def ssm-client (aws/client {:api :ssm}))


(defmethod aero/reader 'parameter
  [_ _ value]
  (-> (aws/invoke ssm-client {:op :GetParameter
                              :request {:Name (name value)}})
      (get-in [:Parameter :Value])))


(defn read-config
  [profile]
  (-> "config.edn"
      io/resource
      (aero/read-config {:profile profile})))


(defn validate-config
  [config valid-config]
  (if (m/validate valid-config config)
    config
    (let [error-message (str (select-keys (m/explain valid-config config) [:errors]))]
      (throw (Exception. error-message)))))


(def system-config
  "A map of all the config parameters required for the system to start."
  [:map [:one int?]])


;; usage
#_(->> :dev
       read-config
       (validate-config system-config))



(defn read-config2
  [{:keys [profile system-config]}]
  (-> "config.edn"
      io/resource
      (aero/read-config {:profile profile})
      (validate-config system-config)))

#_(read-config2 {:profile :dev :system-config system-config})
