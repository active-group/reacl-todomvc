(ns todomvc.components.todo-edit
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.helpers :as helpers]))

(defrecord Blur [])
(defrecord KeyDown [which])
(defrecord Change [text])

(reacl/defclass t this editing [commit-action reset-action]
  refs [input]

  component-did-update
  (fn []
    (.focus (reacl/get-dom input))
    (reacl/return))

  render
  (dom/input {:type "text"
              :class "edit"
              :ref input
              :style {:display (helpers/display-elem editing)}
              :value (or editing "")
              :onblur (fn [e]
                        (reacl/send-message! this (->Blur)))
              :onkeydown (fn [e]
                           (let [key-pressed (.-which e)]
                             (reacl/send-message! this (->KeyDown key-pressed))))
              :onchange (fn [e]
                          (let [text (.. e -target -value)]
                            (reacl/send-message! this (->Change text))))})

  handle-message
  (fn [msg]
    (condp instance? msg
      Blur
      ;; Note: when pressing Return, a blur follows after the keypress (hidden while focused)
      (if editing
        (reacl/return :action commit-action)
        (reacl/return))
      
      Change
      (reacl/return :app-state (:text msg))

      KeyDown
      (let [key-pressed (:which msg)]
        (condp = key-pressed
          helpers/enter-key (reacl/return :action commit-action)
          helpers/escape-key (reacl/return :action reset-action)
          
          (reacl/return))))))
