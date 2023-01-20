#!/usr/bin/env python3

import umbra

args = umbra.parse_arguments()
pg_con = umbra.connect()
con = pg_con.cursor()

if args.weighted:
    weight_attribute_without_type = ", weight"
    weight_attribute_with_type = ", weight FLOAT"
else:
    weight_attribute_without_type = ""
    weight_attribute_with_type = ""

## cleanup
con.execute("DROP TABLE IF EXISTS u")
con.execute("DROP TABLE IF EXISTS v")
con.execute("DROP TABLE IF EXISTS e")

## create schema
con.execute(f"CREATE TABLE v (id INTEGER)")
con.execute(f"CREATE TABLE e (source INTEGER, target INTEGER{weight_attribute_with_type})")

## loading
con.execute(f"COPY v (id) FROM '{umbra.input_data_directory}/{args.graph_name}.v' (DELIMITER ' ', FORMAT csv)")
con.execute(f"COPY e (source, target{weight_attribute_without_type}) FROM '{umbra.input_data_directory}/{args.graph_name}.e' (DELIMITER ' ', FORMAT csv)")

# create undirected variant:
# - for directed graphs, it is an actual table
# - for undirected ones, it is just a view on table e
if args.directed:
    # keep 'e' directed and create table 'u' for accessing an undirected view of the edges
    con.execute(f"CREATE TABLE u (source INTEGER, target INTEGER)")
    con.execute(f"INSERT INTO u SELECT target, source FROM e")
    con.execute(f"INSERT INTO u SELECT source, target FROM e")
else:
    # copy reverse edges to 'e'
    con.execute(f"INSERT INTO e SELECT target, source{weight_attribute_without_type} FROM e")
    # 'u' is just a projection on top of 'e'
    con.execute(f"CREATE TABLE u AS SELECT source, target FROM e")
