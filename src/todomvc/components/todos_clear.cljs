(ns todomvc.components.todos-clear
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(defn t [todos clear-action]
  (u/button {:class "clear-completed"
             :style {:display (helpers/display-elem (helpers/todos-any-completed? todos))}
             :onclick clear-action}
            "Clear completed"))
