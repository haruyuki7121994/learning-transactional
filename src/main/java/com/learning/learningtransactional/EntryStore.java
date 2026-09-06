package com.learning.learningtransactional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EntryStore {
  private final JdbcTemplate jdbc;

  public EntryStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void add(String id) {
    jdbc.update("insert into lab_entry(id,note) values (?,?)", id, "learning");
  }

  public boolean exists(String id) {
    return jdbc.queryForObject("select count(*) from lab_entry where id=?", Integer.class, id) > 0;
  }

  public void clear() {
    jdbc.update("delete from lab_entry");
  }
}
