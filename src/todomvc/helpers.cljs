(ns todomvc.helpers
  (:require [clojure.string :as str]))

(def enter-key 13)

(def escape-key 27)

(defn trim-title [title]
  (str/trim title))

(defn display-elem [bool] 
  (if bool "inline" "none"))

(defn display-item [bool]
  (if bool "list-item" "none"))

(defn todo-display-filter [completed display-type]
  (case display-type
    :completed completed
    :active (not completed)
    true))

;; will look like {id {:title _ :completed _ }}

(def todos-empty (sorted-map))

(defn todos-all [todos]
  (vals todos))

(defn todos-active [todos]
  (let [todos-all (todos-all todos)]
    (filter #(not (:completed %)) todos-all)))

(defn todos-completed-ids [todos]
  (map first (filter (comp :completed second) todos)))

(defn todos-any? [todos]
  (pos? (count (todos-all todos))))

(defn todos-any-completed? [todos]
  (pos? (count (todos-completed-ids todos))))

(defn todos-all-completed? [todos]
  (= (count (todos-all todos))
     (count (todos-completed-ids todos))))
