package com.ryanquey.intertextualitygraph.models;

import com.ryanquey.intertextualitygraph.models.books.BookDao;
/*
 *
 * basically a globally available class so only have to initialize inventoryMapper once, and can access it anywhere to grab daos from
 */

public class InventoryMapperObj {
  public static InventoryMapper inventoryMapper;
}

