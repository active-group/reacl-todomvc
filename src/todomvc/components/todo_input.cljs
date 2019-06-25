(ns todomvc.components.todo-input
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.todo-edit :as todo-edit]
            [todomvc.helpers :as helpers]))

(defrecord Change [text])
(defrecord KeyDown [which])

;; TODO: we have almost the same input in todo_edit.

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
                          (reacl/send-message! this (->Change (.. e -target -value))))
              :onkeydown (fn [e]
                           (reacl/send-message! this (->KeyDown (.-which e))))})

  component-did-mount
  (fn []
    (.focus (reacl/get-dom input))
    (reacl/return))

  handle-message
  (fn [msg]
    (condp instance? msg
      Change
      (reacl/return :local-state (:text msg))
      
      KeyDown
      (let [key-pressed (:which msg)
            title (helpers/trim-title editing)]
        (if (and (= key-pressed helpers/enter-key)
                 (not-empty title))
          (reacl/return :action (add-action title)
                        :local-state "")
          (reacl/return))))))
