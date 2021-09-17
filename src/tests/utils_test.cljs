(ns tests.utils-test
  (:require [cljs.test :refer (deftest run-tests are)]
            [game.util :as util]))

(deftest player?-checks-if-data-is-player
  (are [expected data] (= expected (util/player? data))
    true {:id :player}
    false {:id :1}
    false {:id "player"}
    false nil))

(deftest monster?-checks-if-data-is-monster
  (are [expected data] (= expected (util/monster? data))
    false {:id :player}
    false nil
    true {:id :1}
    true {:id "player"}))

(deftest in?-returns-true-if-collection-contains-element
  (are [expected coll el] (= expected (util/in? coll el))
    true [1] 1
    true [[1]] [1]
    true [1 [1] [[2]]] [[2]]
    nil [1] 2))

(deftest coordinates->i_converts_tilemap_coordinates_to_an_index_value
  (are [expected map_size x y] (= expected (util/coordinates->i map_size x y))
    0 1 0 0
    2 2 0 1
    1 2 1 0
    3 2 1 1
    8 3 2 2))

