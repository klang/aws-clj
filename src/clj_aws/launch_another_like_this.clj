(ns clj-aws.launch-another-like-this
  (:require [aws.sdk.ec2 :as ec2])
  (:use [clj-aws.ec2-client :only (ec2-client)]
        [clj-aws.waiters :only (wait-for-running)]
        [clj-aws.tools :only (legal-tags describe-instance describe-instance-from-name)]))

(comment
  (remove-ns 'clj-aws.launch-another-like-this)
  (def cred {:profile "dashsoft" :endpoint "ec2.eu-west-1.amazonaws.com"})
  (def bastion (clj-aws.tools/describe-instance-from-name cred "microsites-dev-bastion-testing")))

;; TODO - move this to a more logical location

(defn launch-another-like-this
  [cred source-instance-name target-instance-name]
    (let [source (describe-instance-from-name cred source-instance-name)
          params (-> source
                     (select-keys [:image :placement :instance-type :key-name :subnet-id])
                     (clojure.set/rename-keys {:image :image-id})
                     (merge {:min-count 1
                             :max-count 1
                             :security-group-ids (map :id (source :security-groups))}))
          clone (ec2/run-instances cred params)]
      (do
        (wait-for-running cred (->> clone :instances (map :instance-id)))
        (ec2/create-tags
         cred
         (->> clone :instances (map :instance-id))
         (-> source :tags (assoc-in ["Name"] target-instance-name) legal-tags)))))
