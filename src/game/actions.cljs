(ns game.actions
  (:require
   [game.player :as p]))

(defmulti handle :type)

(defn move-player [x y] {:type :MOVE_PLAYER :payload {:x x :y y}})

(defmethod handle :MOVE_PLAYER [action]
  (swap! p/player conj {:x (-> action :payload :x)
                        :y (-> action :payload :y)}))

(defn trigger-actions [actions-list]
  (doseq [action actions-list]
    (handle action)))