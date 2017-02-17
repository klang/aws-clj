(ns clj-aws.waiters
  (:require [aws.sdk.ec2 :as ec2])
  (:use     [clj-aws.ec2-client :only (ec2-client)]
            [clj-aws.tools :only (describe-addresses)])
  ;; http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/overview-summary.html
  (:import com.amazonaws.services.ec2.model.DescribeInstancesRequest
           com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest
           com.amazonaws.waiters.WaiterParameters
           com.amazonaws.waiters.WaiterTimedOutException))

(comment
  (remove-ns 'clj-aws.waiters)
  (def cred {:profile "dashsoft" :endpoint "ec2.eu-west-1.amazonaws.com"})
  (def bastion (clj-aws.tools/describe-instance-from-name cred "microsites-dev-bastion-testing")))

(comment
  (def dir (-> (DescribeInstancesRequest.)
               (.withFilters [(ec2/aws-filter "tag:Name" "microsites-dev-bastion-testing")])))

  (def disr (-> (DescribeInstanceStatusRequest.)
                (.withInstanceIds ["i-099f6bfb5db2fd958"]))))
;; "tag:Name" can not be used with filters on DescribeInstanceStatusRequest
;; check the allowed filters via "aws ec2 wait instance-status-ok help"
;; (.withFilters [(ec2/aws-filter "tag:Name" "microsites-dev-bastion")])

;; https://aws.amazon.com/blogs/developer/waiters-in-the-aws-sdk-for-java/
;; http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/waiters/AmazonEC2Waiters.html
;; http://www.lispcast.com/exponential-backoff
;; http://stackoverflow.com/questions/12068640/retrying-something-3-times-before-throwing-an-exception-in-clojure

(defn wait-for-running [cred instance-ids]
    (-> (ec2-client cred)
        (.waiters)
        (.instanceRunning)      
        (.run
         (WaiterParameters.
          (-> (DescribeInstancesRequest.)
              (.withInstanceIds instance-ids))))))

(defn wait-for-terminated [cred instance-ids]
    (-> (ec2-client cred)
        (.waiters)
        (.instanceTerminated)      
        (.run
         (WaiterParameters.
          (-> (DescribeInstancesRequest.)
              (.withInstanceIds instance-ids))))))

(defn wait-for-stopped
  "use (wait-for-stopped cred [instance-id]) if only one instance-id is needed"
  [cred instance-ids]
    (-> (ec2-client cred)
        (.waiters)
        (.instanceStopped)      
        (.run
         (WaiterParameters.
          (-> (DescribeInstancesRequest.)
              (.withInstanceIds instance-ids))))))

(defn wait-for-status-ok [cred instance-ids]
  (-> (ec2-client cred)
      (.waiters)
      (.instanceStatusOk)      
      (.run
       (WaiterParameters.
        (-> (DescribeInstanceStatusRequest.)
            (.withInstanceIds instance-ids))))))

;; http://stackoverflow.com/questions/12068640/retrying-something-3-times-before-throwing-an-exception-in-clojure
;; but.. instead of trying again because of an exception, we just wait and try again because the expr is expected
;; to be an external status check that can change
(defmacro retry-sleep
  "cnt retries of expr with a sleep delay between executions of expr."
  [cnt sleep expr]
  (letfn [(go [cnt]
              (if (zero? cnt)
                expr
                `(let [~'result ~expr]
                   (if (not ~'result)
                     (do (Thread/sleep ~sleep)
                         (retry-sleep ~(dec cnt) ~sleep ~expr))
                     ~'result))))]
    (go cnt)))

;; there does not seem to be an easy way to wait for an eip to be available
(defn is-eip-available? [cred public-ip-address]
  (nil? (->> (describe-addresses cred public-ip-address) :addresses first :instance-id)))

(defn wait-for-eip [cred public-ip-address]
  (if (not (retry-sleep 5 2000 (is-eip-available? cred public-ip-address)))
    (throw (WaiterTimedOutException. "Reached maximum attempts without finding the desired eip "))))

