(ns clj-aws.ec2-client
  (:import 
           com.amazonaws.auth.profile.ProfileCredentialsProvider
           ;;com.amazonaws.auth.BasicAWSCredential
           com.amazonaws.auth.BasicSessionCredentials
           com.amazonaws.auth.DefaultAWSCredentialsProviderChain
           com.amazonaws.auth.BasicAWSCredentials
           com.amazonaws.services.ec2.AmazonEC2Client))

(comment
  (remove-ns 'clj-aws.ec2-client)
  (def cred {:profile "dashsoft" :endpoint "ec2.eu-west-1.amazonaws.com"}))

(defn- ec2-client*
  "Create an AmazonEC2Client instance from a map of credentials."
  [{:keys [access-key secret-key endpoint profile]}]
  (let [credentials (if (and access-key secret-key)
                      (BasicAWSCredentials. access-key secret-key)
                      (if profile
                        (ProfileCredentialsProvider. profile)
                        (DefaultAWSCredentialsProviderChain.)))
        client (AmazonEC2Client. credentials)]
    (if endpoint (.setEndpoint client endpoint))
    client))

(def ^{:private false}
  ec2-client
  (memoize ec2-client*))


