package com.tsAdmin.control;

import com.jfinal.plugin.activerecord.Db;

public class UpdateGlobal 
{
    // 查询 count 的值
    public static int getCountFromDb() {
        // 查询 global_table 表中 id = 1 的记录
        String sql = "SELECT count FROM global WHERE id = ?";
        Integer count = Db.queryInt(sql, 0); // 假设 global_table 的主键 id 为 0
        return count != null ? count : 0; // 如果没有记录，返回 0
    }

    // 将 count 的值 +1 并更新到数据库
    public static boolean incrementCountInDb() {
        // 查询当前的 count 值
        int currentCount = getCountFromDb();
        int newCount = currentCount + 1;

        // 更新数据库中的 count 值
        String updateSql = "UPDATE global SET count = ? WHERE id = ?";
        int rowsUpdated = Db.update(updateSql, newCount, 0); // 假设 id 为 0
        return rowsUpdated > 0; // 返回是否更新成功
    }
}
