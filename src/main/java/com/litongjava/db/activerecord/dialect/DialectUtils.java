package com.litongjava.db.activerecord.dialect;

import java.util.List;
import java.util.Map.Entry;

import com.litongjava.db.activerecord.Record;

public class DialectUtils {

  /**
   * 一、forDbXxx 系列方法中若有如下两种情况之一，则需要调用此方法对 pKeys 数组进行 trim():
   * 1：方法中调用了 isPrimaryKey(...)：为了防止在主键相同情况下，由于前后空串造成 isPrimaryKey 返回 false
   * 2：为了防止 tableName、colName 与数据库保留字冲突的，添加了包裹字符的：为了防止串包裹区内存在空串
   *   如 mysql 使用的 "`" 字符以及 PostgreSql 使用的 "\"" 字符
   * 不满足以上两个条件之一的 forDbXxx 系列方法也可以使用 trimPrimaryKeys(...) 方法让 sql 更加美观，但不是必须
   * 
   * 二、forModelXxx 由于在映射时已经trim()，故不再需要调用此方法
   */
  public static void trimPrimaryKeys(String[] pKeys) {
    for (int i = 0; i < pKeys.length; i++) {
      pKeys[i] = pKeys[i].trim();
    }
  }

  public static String forDbFindColumnsById(String tableName, String columns, String[] pKeys) {
    StringBuilder sql = forDbFindColumnsReturnStrinBuilder(tableName, columns);
    sql.append(" where ");
    for (int i = 0; i < pKeys.length; i++) {
      if (i > 0) {
        sql.append(" and ");
      }
      sql.append('`').append(pKeys[i]).append("` = ?");
    }
    return sql.toString();
  }

  public static String forDbFindColumns(String tableName, String columns) {
    return forDbFindColumnsReturnStrinBuilder(tableName, columns).toString();
  }

  public static StringBuilder forDbFindColumnsReturnStrinBuilder(String tableName, String columns) {
    StringBuilder sql = new StringBuilder("select ");
    columns = columns.trim();
    if ("*".equals(columns)) {
      sql.append('*');
    } else {
      String[] arr = columns.split(",");
      for (int i = 0; i < arr.length; i++) {
        if (i > 0) {
          sql.append(',');
        }
        sql.append('`').append(arr[i].trim()).append('`');
      }
    }

    sql.append(" from `").append(tableName).append("`");
    return sql;
  }

  public static void forDbDelete(String tableName, String[] pKeys, Record record, StringBuilder sql,
      List<Object> paras) {
    tableName = tableName.trim();
    trimPrimaryKeys(pKeys); // important

    sql.append("delete from `");
    sql.append(tableName).append("`");

    sql.append(" where ");

    int i = 0;
    for (Entry<String, Object> e : record.getColumns().entrySet()) {
      if (i > 0) {
        sql.append(" and ");
      }
      sql.append('`').append(e.getKey()).append('`').append("=? ");
      paras.add(e.getValue());
      i++;
    }
  }

  public static String forExistsByFields(String tableName, String fields) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("select count(1) from `").append(tableName).append("`");
    String[] split = fields.split(",");
    if (split.length > 0) {
      stringBuffer.append(" where ");
      for (String field : split) {
        stringBuffer.append('`').append(field.trim()).append('`').append("= ?");
      }
    }
    return stringBuffer.toString();
  }

}
