(ns game.potion-behaviours
  (:require [game.state :refer [update-entity-stat]]))

(defmulti do-effect
  "Triggers a potions specific effect...
   
   :STAT-CHANGE - change a numeric entity stat by an amount"
  (fn [_ effect] (:effect effect)))

(defmethod do-effect :STAT-CHANGE [entity-id effect]
  (update-entity-stat entity-id (:stat effect) (:amount effect)))