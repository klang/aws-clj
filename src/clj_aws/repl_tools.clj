(ns clj-aws.repl-tools
  (:use [clojure.pprint :only (pprint)]
        [clojure.reflect :only (reflect)])
  (:require [clojure.string :as str]))

(defn all-methods [x]
  (->> x
       reflect 
       :members 
       (filter :return-type)  
       (map :name) 
       sort 
       (map #(str "." %) )
       distinct
       println))

