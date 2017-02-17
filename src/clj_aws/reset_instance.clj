(ns clj-aws.reset-instance
  (:require [aws.sdk.ec2 :as ec2])
  (:use [clj-aws.ec2-client :only (ec2-client)]
        [clj-aws.waiters :only (wait-for-running wait-for-terminated wait-for-eip)]
        [clj-aws.tools :only (legal-tags
                              #_describe-disable-api-termination
                              #_modify-disable-api-termination
                              associate-address
                              describe-addresses
                              describe-instance)]))

(comment
  (remove-ns 'clj-aws.reset-instance)
  (def cred {:profile "dashsoft" :endpoint "ec2.eu-west-1.amazonaws.com"})
  (def bastion (clj-aws.tools/describe-instance-from-name cred "microsites-dev-bastion-testing")))

;; TODO - consider api-termination when resetting an instance

(defn reset-instance
  [cred instance-id & [{:keys [image-id instance-type]}]]
  (let [source (describe-instance cred instance-id)
        params (-> source
                   (select-keys [:private-ip-address :placement :key-name :subnet-id :instance-type :image])
                   (clojure.set/rename-keys {:image :image-id})
                   (merge {:min-count          1
                           :max-count          1
                           :security-group-ids (map :group-id (source :security-groups))
                           :image-id           (if image-id image-id (source :image))
                           :instance-type      (if instance-type instance-type (source :instance-type))}))
        terminating (do
                      (ec2/terminate-instances cred instance-id)
                      (wait-for-terminated cred [instance-id]))
        clone (ec2/run-instances cred params)]
    (do
      (wait-for-running
       cred
       (->> clone :instances (map :instance-id)))
      (ec2/create-tags
       cred
       (->> clone :instances (map :instance-id))
       (-> source :tags legal-tags))
      (if-let [public-ip-address (:public-ip-address source)]
        (do
          (try
            (wait-for-eip cred public-ip-address)
            (associate-address
             cred
             {:instance-id (-> clone :instances first :instance-id)
              :allocation-id (-> (describe-addresses cred public-ip-address)
                                 :addresses first :allocation-id)
              :allow-reassociation true})))))
    (-> clone :instances first :instance-id)))


