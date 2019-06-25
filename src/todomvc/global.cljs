(ns todomvc.global
  (:require [alandipert.storage-atom :refer [local-storage]]
            [reacl2.core :as reacl :include-macros true]
            [todomvc.helpers :as helpers]))

(reset! alandipert.storage-atom/storage-delay 0)

(def todos (local-storage (atom helpers/todos-empty) :todos-reacl))
;; will look like {id {:id _ :title _ :completed _ }}

(def todos-counter (local-storage (atom 0) :todos-counter-reacl))
;; will inc for each new todo

(defrecord ResetStorage [atom value])

(defn reset-action [atom]
  (fn [value]
    (->ResetStorage atom value)))

(defrecord FocusElement [elem])

(defn focus-action [elem]
  (->FocusElement elem))

(defn handle-global-action! [app-state action]
  (condp instance? action
    ResetStorage (do (reset! (:atom action) (:value action))
                     (reacl/return))
    FocusElement (do (.focus (:elem action))
                     (reacl/return))))
