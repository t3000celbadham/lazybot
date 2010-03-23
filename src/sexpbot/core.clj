(ns sexpbot.core
  (:use (sexpbot.plugins utils eball google lmgtfy translate eval)
	sexpbot.respond
	[clojure.contrib.str-utils :only [re-split]])
  (:import (org.jibble.pircbot PircBot)))

(def prepend \$)
(def server "irc.freenode.net")
(def channels ["#()" "#clojure-casual"])

(defn wall-hack-method [class-name name- params obj & args]
  (-> class-name (.getDeclaredMethod (name name-) (into-array Class params))
    (doto (.setAccessible true))
    (.invoke obj (into-array Object args))))

(defn split-args [s] (let [[command & args] (re-split #" " s)]
		       {:command command
			:first (first command)
			:args args}))

(defn make-bot [] 
  (let [bot (proxy [PircBot] []
	      (onMessage 
	       [chan send login host mess]
	       (if (= (first mess) prepend)
		 (respond (merge (split-args (apply str (rest mess)))
				 {:bot this 
				  :sender send 
				  :channel chan 
				  :login login 
				  :host host})))))]
    (wall-hack-method PircBot :setName [String] bot "sexpbot")
    (doto bot
      (.setVerbose true)
      (.connect server))
    (doseq [chan channels] (.joinChannel bot chan))))

(make-bot) 
