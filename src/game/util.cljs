(ns game.util)

(defn player? [entity]
  (= :player (:id entity)))

(defn monster? [entity]
  (and (some? entity) (not (player? entity))))

(defn in? [coll element]
  (some #(= element %) coll))