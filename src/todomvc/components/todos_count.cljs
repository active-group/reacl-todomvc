(ns todomvc.components.todos-count
  (:require [reacl2.dom :as dom]
            [todomvc.helpers :as helpers]))

(defn items-left [todos]
  (let [active-count (count (helpers/todos-active todos))]
    (str (if (= 1 active-count) " item " " items ")
         "left")))

(defn t [todos]
  (dom/span {:class "todo-count"}
            (dom/strong (count (helpers/todos-active todos)))
            (items-left todos)))
