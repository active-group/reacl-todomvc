(ns todomvc.components.footer
  (:require [reacl2.dom :as dom]))

(defn t []
  (dom/footer {:class "info"}
              (dom/p "Double-click to edit a todo")
              (dom/p "Credits: To the authors of the reagent todo mvc.")
              (dom/p "Part of" (dom/a {:href "http://todomvc.com"} "TodoMVC"))))
