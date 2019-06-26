(ns todomvc.components.todos-toggle
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(reacl/defclass t this [todos toggle-action]
  render
  (dom/span {:class "toggle"}
            (u/checkbox (reacl/reactive (helpers/todos-all-completed? todos)
                                        (reacl/pass-through-reaction this))
                        {:id "toggle-all"
                         :class "toggle-all"})
            (dom/label {:htmlFor "toggle-all"} "Mark all as complete"))

  handle-message
  (fn [toggle]
    (toggle-action toggle)))
