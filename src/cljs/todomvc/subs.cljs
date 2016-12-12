(ns todomvc.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :showing
 (fn [db _]
   (:showing db)))

(defn sorted-todos
  [db _]
  (:todos db))

(reg-sub :sorted-todos sorted-todos)


(reg-sub
 :todos
 (fn [query-v _]
   (subscribe [:sorted-todos]))
 (fn [sorted-todos query-v _]
   (vals sorted-todos)))

(reg-sub
 :visible-todos
 :<- [:todos]
 :<- [:showing]
 ;; (fn [query-v _]
 ;;   [(subscribe [:todos])
 ;;    (subscribe [:showing])])

 (fn [[todos showing] _]
   (let [filter-fn (case showing
                     :active (complement :done)
                     :done :done
                     :all identity)]
     (filter filter-fn todos ))))

(reg-sub
 :all-complete?
 :<- [:todos]
 (fn [todos _]
   (seq todos)))

(reg-sub
 :completed-count
 :<- [:todos]
 (fn [todos _]
   (count (filter :done todos))))

(reg-sub
 :footer-counts
 :<- [:todos]
 :<- [:completed-count]
 (fn [[todos completed] _]
   [(- (count todos) completed) completed]))
