(ns clj-aws.cred
  (:require [clojure.string :as str])
  (:import com.amazonaws.auth.profile.ProfileCredentialsProvider))

;; ~/.boto
;; [Credentials]
;; aws_access_key_id = something
;; aws_secret_access_key = something

(def ^:private boto
  (->> (slurp "/Users/klang/.boto-dashsoftexp")
       str/split-lines
       (map #(str/split % #" = "))
       (filter #(= 2 (count %)))
       (map #(hash-map (keyword (first %)) (second %)))
       (into {})))

(def boto-cred
  {:access-key (boto :aws_access_key_id)
   :secret-key (boto :aws_secret_access_key)})

;; no need to memoize the provider
;; memoize the credentials instead
(comment (defn ^:private provider* [profile]
           (ProfileCredentialsProvider. profile))

         (def provider (memoize provider*)))

(defn provider [profile]
  (ProfileCredentialsProvider. profile))

(defn endpoint [service region]
  {:endpoint (str service"."region".amazonaws.com")})

(defn ^:private credentials* [profile]
    (let [provider (provider profile)]
      {:access-key (.. provider getCredentials getAWSAccessKeyId)
       :secret-key (.. provider getCredentials getAWSSecretKey)}))

(def ^{:private false}
  credentials
  (memoize credentials*))

(def cred
  (merge (credentials "dashsoft") (endpoint "ec2" "eu-west-1")))
