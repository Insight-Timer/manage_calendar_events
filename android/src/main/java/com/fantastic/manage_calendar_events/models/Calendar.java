package com.fantastic.manage_calendar_events.models;
import com.google.gson.annotations.SerializedName;

public final class Calendar {

  @SerializedName("id")
  private final String id;
  @SerializedName("name")
  private final String name;
  @SerializedName("accountName")
  private final String accountName;
  @SerializedName("ownerName")
  private final String ownerName;
  @SerializedName("isReadOnly")
  private final boolean isReadOnly;

  public Calendar(String id, String name, String accountName, String ownerName, boolean isReadOnly) {
    this.id = id;
    this.name = name;
    this.accountName = accountName;
    this.ownerName = ownerName;
    this.isReadOnly = isReadOnly;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAccountName() {
    return accountName;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public Boolean getIsReadOnly() {
    return isReadOnly;
  }

  @Override
  public String toString() {
    return new StringBuffer().append(id).append("-").append(name).append("-").append(accountName)
        .append("-")
        .append(ownerName)
        .append("-")
        .append(isReadOnly)
        .toString();
  }
}
