(defproject clj-aws "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.xmlunit/xmlunit-core "2.3.0"]
                 [clj-ssh "0.5.11"]
                 [net.schmizz/sshj "0.10.0"]
                 [com.amazonaws/aws-java-sdk "1.11.86" :exclusions [joda-time]]
                 [clj-aws-ec2 "0.5.0"]
                 [clj-aws-s3 "0.3.10" :exclusions [joda-time]]])

;; S3       https://github.com/weavejester/clj-aws-s3
;; EC2      https://github.com/mrowe/clj-aws-ec2
;; DynamoDB https://github.com/ptaoussanis/faraday
;;          https://github.com/weavejester/rotary
;; SQS      https://github.com/cemerick/bandalore
;; Route53  https://github.com/mrowe/clj-aws-r53 -- doesn't work
;;          https://github.com/arohner/clj-r53
;; json     https://github.com/clojure/data.json
;; xml      https://github.com/clojure/data.xml
;; xmlunit  https://github.com/xmlunit/xmlunit
