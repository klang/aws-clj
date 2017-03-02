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
    [{:keys [access-key secret-key endpoint profile assume-role-result]}]
    (let [credentials (cond assume-role-result
                            (BasicSessionCredentials.
                             (-> assume-role-result :credentials :access-key-id )
                             (-> assume-role-result :credentials :secret-access-key)
                             (-> assume-role-result :credentials :session-token))
                            (and access-key secret-key)
                            (BasicAWSCredentials. access-key secret-key)
                            profile
                            (ProfileCredentialsProvider. profile)
                            :else
                            (DefaultAWSCredentialsProviderChain.))
          client (AmazonEC2Client. credentials)]
      (if endpoint (.setEndpoint client endpoint))
      client))

(def ^{:private false}
  ec2-client
  (memoize ec2-client*))


