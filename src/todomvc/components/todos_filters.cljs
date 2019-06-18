(ns todomvc.components.todos-filters
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]))

(defn selected-class [display-type todos-display-type]
  (if (= display-type
         todos-display-type)
    "selected" ""))

(defn t [display-type]
  (dom/ul {:class "filters"}
          (map (fn [[t href title]]
                 (dom/li {:key t} (dom/a {:class (selected-class t display-type) :href href} title)))
               [[:all "#/" "All"]
                [:active "#/active" "Active"]
                [:completed "#/completed" "Completed"]])))
