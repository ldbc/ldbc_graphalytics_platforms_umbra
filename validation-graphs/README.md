- add a header (`v,out`)
- add a comma after the first number
- run duckdb with commands like this:

    ```sql
    D create or replace table e as select * from read_csv_auto("pr-directed-test-graph.e");
    D copy (select * from (select v, unnest(str_split(out, ' ')) as x from e) sub) to 'my.e' with (delimiter ' ');
    ```

    ```sql
    D create or replace table e as select * from read_csv_auto("pr-undirected-test-graph.e");
    D copy (select * from (select v,unnest(str_split(out, ' ')) as x from e) sub where v::int <= x::int) to 'my.e' with (delimiter ' ');
    ```

- check output and copy its content to overwrite the original `.e` file
