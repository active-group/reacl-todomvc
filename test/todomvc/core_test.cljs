(ns todomvc.core-test
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [reacl2.test-util.beta :as tu]
            [todomvc.core :as core]
            [todomvc.global :as global]
            [todomvc.helpers :as helpers])
  (:require-macros [cljs.test :refer (is deftest testing)]))

(deftest test-todoapp
  (let [c (tu/env core/todo-app)]
    (is (= (tu/mount! c {:todos helpers/todos-empty
                         :counter 1}
                      :all)
           (reacl/return :action (global/focus-action nil))))

    (is (= (tu/send-message! c (core/->Add "Hello"))
           (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed false}}
                                     :counter 2})))

    (tu/update! c {:todos {:id1 {:title "Hello" :completed false}}
                   :counter 2}
                :all)
    (is (= (tu/send-message! c (core/->Update :id1 {:title "World" :completed true}))
           (reacl/return :app-state {:todos {:id1 {:title "World" :completed true}}
                                     :counter 2})))

    (is (= (tu/send-message! c (core/->Delete :id1))
           (reacl/return :app-state {:todos {}
                                     :counter 2})))

    (is (= (tu/send-message! c (core/->ClearCompleted))
           (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed false}}
                                     :counter 2})))

    (tu/update! c {:todos {:id1 {:title "Hello" :completed true}}
                   :counter 2}
                :all)
    (is (= (tu/send-message! c (core/->ClearCompleted))
           (reacl/return :app-state {:todos {}
                                     :counter 2})))

    (is (= (tu/send-message! c (core/->ToggleAll false))
           (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed false}}
                                     :counter 2})))
    
    ))
