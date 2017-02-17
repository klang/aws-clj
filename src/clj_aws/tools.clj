(ns clj-aws.tools
  (:use [clojure.pprint :only (pprint)]
        [aws.sdk.ec2 :only (to-map Mappable)]
        [clj-aws.ec2-client :only (ec2-client)]
        [clj-aws.cred :only (provider credentials endpoint)]
        [clj-aws.repl-tools :only (all-methods)])
  (:require [aws.sdk.ec2 :as ec2])
  (:import com.amazonaws.AmazonServiceException
           com.amazonaws.auth.profile.ProfileCredentialsProvider
           ;;com.amazonaws.auth.BasicAWSCredential
           com.amazonaws.auth.BasicSessionCredentials
           com.amazonaws.auth.DefaultAWSCredentialsProviderChain
           com.amazonaws.auth.BasicAWSCredentials
           com.amazonaws.services.ec2.AmazonEC2Client
           com.amazonaws.services.ec2.model.DescribeAddressesRequest
           com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest
           com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest
           com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult
           com.amazonaws.services.ec2.model.InstancePrivateIpAddress
           com.amazonaws.services.ec2.model.IamInstanceProfile
           com.amazonaws.services.ec2.model.InstanceNetworkInterface
           com.amazonaws.services.ec2.model.InstanceNetworkInterfaceAttachment
           com.amazonaws.services.ec2.model.InstanceNetworkInterfaceAssociation
           com.amazonaws.services.ec2.model.AssociateAddressRequest
           com.amazonaws.services.ec2.model.AssociateAddressResult
           com.amazonaws.services.ec2.model.DisassociateAddressRequest
           ;;com.amazonaws.services.ec2.model.DisassociateAddressResult
           com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
           com.amazonaws.services.securitytoken.model.AssumeRoleRequest
           com.amazonaws.services.securitytoken.model.AssumeRoleResult
           com.amazonaws.services.ec2.waiters.AmazonEC2Waiters
           com.amazonaws.services.ec2.model.DescribeInstancesRequest
           com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest
           com.amazonaws.waiters.WaiterParameters))
(comment
  (remove-ns 'clj-aws.tools))

(extend-protocol Mappable
  com.amazonaws.services.securitytoken.model.AssumeRoleResult
  (to-map [r] {:assumed-role-user (to-map (.getAssumedRoleUser r))
               :credentials (to-map (.getCredentials r))
               :packed-policy-size (.getPackedPolicySize r)})
  com.amazonaws.services.securitytoken.model.AssumedRoleUser
  (to-map [r] {:arn (.getArn r)
               :assumed-role-id (.getAssumedRoleId r)})
  com.amazonaws.services.securitytoken.model.Credentials
  (to-map [r] {:access-key-id (.getAccessKeyId r)
               :expiration  (.getExpiration r)
               :secret-access-key (.getSecretAccessKey r)
               :session-token (.getSessionToken r) }))

(extend-protocol Mappable  
  com.amazonaws.auth.profile.ProfileCredentialsProvider
  (to-map [r] {:credentials (to-map (.getCredentials r))})
  com.amazonaws.auth.BasicAWSCredentials
  (to-map [r] {:access-key (.getAWSAccessKeyId r)
               :secret-key (.getAWSSecretKey r)})
  com.amazonaws.auth.DefaultAWSCredentialsProviderChain
  (to-map [r] {:credentials (to-map (.getCredentials r))
               :reuse-last-provider (.getReuseLastProvider r)}))

(extend-protocol Mappable
  com.amazonaws.services.ec2.model.GroupIdentifier
  (to-map [r] {:group-name (.getGroupName r)
               :group-id (.getGroupId r)})
  com.amazonaws.services.ec2.model.InstancePrivateIpAddress
  (to-map [r] {:private-ip-address (.getPrivateIpAddress r)
               :is-primary (.isPrimary r)
               :association (to-map (.getAssociation r))})
  com.amazonaws.services.ec2.model.InstanceNetworkInterfaceAssociation
  (to-map [r] {:public-dns-name (.getPublicDnsName r)
               ;;:ip-owner-id (.getIpOwnerId r)
               :public-ip (.getPublicIp r)})
  com.amazonaws.services.ec2.model.InstanceNetworkInterfaceAttachment
  (to-map [r] {:attachment-id (.getAttachmentId r)
               :device-index (.getDeviceIndex r)
               :status (.getStatus r)
               :attach-time (.getAttachTime r)
               :delete-on-termination (.getDeleteOnTermination r)})
  com.amazonaws.services.ec2.model.InstanceNetworkInterface
  (to-map [r] {:network-interface-id (.getNetworkInterfaceId r)
               :subnet-id	(.getSubnetId r)
               :vpc-id	(.getVpcId r)
               :association (to-map (.getAssociation r))
               ;;:attachment (to-map (.getAttachment r))
               ;;:description 	(.getDescription  r)
               ;;:owner-id	(.getOwnerId r)
               :status 	(.getStatus  r)
               ;;:mac-address	(.getMacAddress r)
               ;;:source-dest-check	(.getSourceDestCheck r)
               ;:private-ip-address 	(.getPrivateIpAddress  r)
               :private-dns-name (.getPrivateDnsName r)
               :private-ip-address 	(.getPrivateIpAddress  r)
               ;;:private-ip-addresses 	(map to-map (.getPrivateIpAddresses  r))
               :ipv6-addresses	(.getIpv6Addresses r)
               :groups (map to-map (.getGroups r))
               }))

(extend-protocol Mappable
  com.amazonaws.services.ec2.model.Instance
  (to-map [instance]
    ;; consistent naming of several return values
    {:instance-id           (.getInstanceId instance)
     :state                 (to-map (.getState instance))
     :instance-type         (.getInstanceType instance)
     :placement             (to-map (.getPlacement instance))
     :tags                  (reduce merge (map to-map (.getTags instance)))
     :image                 (.getImageId instance)
     :key-name              (.getKeyName instance)
     :subnet-id             (.getSubnetId instance)
     :security-groups       (map to-map (.getSecurityGroups instance))
     :public-dns-name       (.getPublicDnsName instance)
     :private-ip-address    (.getPrivateIpAddress instance)
     :public-ip-address     (.getPublicIpAddress instance)
     :iam-instance-profile  (to-map (.getIamInstanceProfile instance))
     :network-interfaces    (map to-map (.getNetworkInterfaces instance))
     :block-device-mappings (map to-map (.getBlockDeviceMappings instance))
     :launch-time           (.getLaunchTime instance)}))

(extend-protocol Mappable
  com.amazonaws.services.ec2.model.IamInstanceProfile
  (to-map [r]
    {:arn (.getArn r)
     :id  (.getId r)})
  com.amazonaws.services.ec2.model.DescribeAddressesResult
  (to-map [r]
    {:addresses             (map to-map (.getAddresses r))})
  com.amazonaws.services.ec2.model.Address
  (to-map [r]
    {:public-ip             (.getPublicIp r)
     :allocation-id         (.getAllocationId r)
     :domain                (.getDomain r)
     ;; :network-interface-id       (.getNetworkInterfaceId r)
     ;; :network-interface-owner-id (.getNnetworkInterfaceOwnerId r)
     ;; :private-ip-address         (.getPrivateIpAddress r)
     :instance-id           (.getInstanceId r)})
  com.amazonaws.services.ec2.model.AssociateAddressResult
  (to-map [r]
    {:association-id (.getAssociationId r)}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn associate-address
  "Associate an ip address to an instance 

  (ec2/associate-address cred { :instance-id \"i-6eb525c4\"
                                :allocation-id \"eipalloc-50ca3935\"
                                :allow-reassociation true })
  
  See
  http://docs.amazonwebservices.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/model/AssociateAddressRequest.html
  for a complete list of available parameters."
  [cred params]
  (to-map (.associateAddress
           (ec2-client cred)
           ((ec2/mapper-> AssociateAddressRequest) params))))

(defn disassociate-address
  "Disassociate an ip address to an instance 

  (ec2/disassociate-address cred { :public-ip \"52.17.171.110\"
                                   :association-id \"???\" })
  
  See
  http://docs.amazonwebservices.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/model/DisassociateAddressRequest.html
  for a complete list of available parameters."
  [cred params]
  (to-map (.disassociateAddress
           (ec2-client cred)
           ((ec2/mapper-> DisassociateAddressRequest) params))))

(defn describe-addresses [cred public-ip]
  (to-map
   (.describeAddresses (ec2-client cred) 
                       (-> (DescribeAddressesRequest.)
                           (.withFilters [(ec2/aws-filter "public-ip" public-ip)])))))

(defn available-elastic-ips [cred]
  (filter #(nil? (:instance-id %))
          (:addresses
           (to-map (.describeAddresses
                    (ec2-client cred) 
                    (DescribeAddressesRequest.))))))

(defn describe-instance
  "will return the "
  [cred instance-id]
  (let [instance (ec2/describe-instances
                  cred
                  (ec2/instance-filter
                   (ec2/aws-filter "instance-id" instance-id)))]
    #_(println (->> instance first :instances first :state :name))
    (->> instance first :instances first )))

(defn describe-instance-from-name
  "will return the first instance with a specific name"
  [cred instance-name]
  (let [instances (ec2/describe-instances
                   cred
                   (ec2/instance-filter
                    (ec2/aws-filter "tag:Name" instance-name)))
        instance (filter #(not (= "terminated"
                                  (->> % :instances first :state :name))) instances)]
    #_(println (->> instance first :instances first :state :name))
    (->> instance first :instances first )))

(defn describe-disable-api-termination [cred instance-id]
  (.getDisableApiTermination
   (.getInstanceAttribute
    (.describeInstanceAttribute
     (ec2-client cred)
     (DescribeInstanceAttributeRequest. instance-id "disableApiTermination")))))

(defn modify-disable-api-termination [cred instance-id ^Boolean value]
  (.modifyInstanceAttribute
   (ec2-client cred)
   (-> (ModifyInstanceAttributeRequest.)
       (.withInstanceId instance-id)
       (.withDisableApiTermination value)))
  (describe-disable-api-termination cred instance-id))

(defn legal-tags
    "Tags stating with 'aws:' are not reserved for AWS internal use."
    [tags]
    (select-keys tags
                 (->> tags
                      (filter #(not (.startsWith (key %) "aws:")) )
                      (map first))))

