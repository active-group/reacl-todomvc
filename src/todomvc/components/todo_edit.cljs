(ns todomvc.components.todo-edit
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.global :as global]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(defrecord Blur [])
(defrecord KeyDown [which])

(defn editing-text-lens
  ([v] (or v ""))
  ([o v] v))

(reacl/defclass t this editing [commit-action reset-action]
  refs [input]

  component-did-update
  (fn []
    (let [elem (reacl/get-dom input)]
      (reacl/return :action (global/focus-action elem))))

  render
  (u/text-input (reacl/bind this editing-text-lens)
                {:class "edit"
                 :ref input
                 :style {:display (helpers/display-elem editing)}
                 :onblur (reacl/return :message [this (->Blur)])
                 :onkeydown (u/event-message this ->KeyDown)})

  handle-message
  (fn [msg]
    (condp instance? msg
      Blur
      ;; Note: when pressing Return, a blur follows after the keypress (hidden while focused)
      (if editing
        commit-action
        (reacl/return))
      
      KeyDown
      (let [key-pressed (:which msg)]
        (condp = key-pressed
          helpers/enter-key commit-action
          helpers/escape-key reset-action
          
          (reacl/return))))))
