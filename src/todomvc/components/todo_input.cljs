(ns todomvc.components.todo-input
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.todo-edit :as todo-edit]
            [todomvc.helpers :as helpers]))

(reacl/defclass t this [add-action]
  refs [input]

  local-state [editing ""]

  render
  (dom/input {:type "text"
              :class "new-todo"
              :ref input
              :value editing
              :placeholder "What needs to be done?"
              :onchange (fn [e]
                          (reacl/send-message! this [::change (.. e -target -value)]))
              :onkeydown (fn [e]
                           (reacl/send-message! this [::key (.-which e)]))})

  component-did-mount
  (fn []
    (.focus (reacl/get-dom input))
    (reacl/return))

  handle-message
  (fn [msg]
    (cond
      (= (first msg) ::change)
      (reacl/return :local-state (second msg))
      
      (= (first msg) ::key)
      (let [key-pressed (second msg)
            title (helpers/trim-title editing)]
        (if (and (= key-pressed helpers/enter-key)
                 (not-empty title))
          (reacl/return :action (add-action title)
                        :local-state "")
          (reacl/return))))))
