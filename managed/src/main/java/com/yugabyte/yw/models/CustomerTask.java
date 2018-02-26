// Copyright (c) YugaByte, Inc.

package com.yugabyte.yw.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.yugabyte.yw.commissioner.tasks.CreateBackup;
import play.data.validation.Constraints;

@Entity
public class CustomerTask extends Model {
  public enum TargetType {
    @EnumValue("Universe")
    Universe,

    @EnumValue("Table")
    Table,

    @EnumValue("Provider")
    Provider,

    @EnumValue("Node")
    Node,

    @EnumValue("Backup")
    Backup
  }

  public enum TaskType {
    @EnumValue("Create")
    Create,

    @EnumValue("Update")
    Update,

    @EnumValue("Delete")
    Delete,

    @EnumValue("Stop")
    Stop,

    @EnumValue("Start")
    Start,

    @EnumValue("Remove")
    Remove,

    @EnumValue("Add")
    Add,

    @EnumValue("Release")
    Release,

    @EnumValue("UpgradeSoftware")
    UpgradeSoftware,

    @EnumValue("UpgradeGflags")
    UpgradeGflags,

    @EnumValue("BulkImportData")
    BulkImportData;

    public String toString(boolean completed) {
      switch(this) {
        case Create:
          return completed ? "Created " : "Creating ";
        case Update:
          return completed ? "Updated " : "Updating ";
        case Delete:
          return completed ? "Deleted " : "Deleting ";
        case UpgradeSoftware:
          return completed ? "Upgraded Software " : "Upgrading Software ";
        case UpgradeGflags:
          return completed ? "Upgraded GFlags " : "Upgrading GFlags ";
        case BulkImportData:
          return completed ? "Bulk imported data" : "Bulk importing data";
        default:
          return null;
      }
    }
  }

  @Id @GeneratedValue
  private Long id;

  @Constraints.Required
  @Column(nullable = false)
  private UUID customerUUID;
  public UUID getCustomerUUID() { return customerUUID; }

  @Constraints.Required
  @Column(nullable = false)
  private UUID taskUUID;
  public UUID getTaskUUID() { return taskUUID; }

  @Constraints.Required
  @Column(nullable = false)
  private TargetType targetType;
  public TargetType getTarget() { return targetType; }

  @Constraints.Required
  @Column(nullable = false)
  private String targetName;
  public String getTargetName() { return targetName; }

  @Constraints.Required
  @Column(nullable = false)
  private TaskType type;
  public TaskType getType() { return type; }

  @Constraints.Required
  @Column(nullable = false)
  private UUID targetUUID;
  public UUID getTargetUUID() { return targetUUID; }

  @Constraints.Required
  @Column(nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
  private Date createTime;
  public Date getCreateTime() { return createTime; }

  @Column
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
  private Date completionTime;
  public Date getCompletionTime() { return completionTime; }
  public void markAsCompleted() {
    if (completionTime == null) {
      completionTime = new Date();
      save();
    }
  }

  public static final Find<Long, CustomerTask> find = new Find<Long, CustomerTask>(){};

  public static CustomerTask create(Customer customer, UUID targetUUID, UUID taskUUID,
                                    TargetType targetType, TaskType type, String targetName) {
    CustomerTask th = new CustomerTask();
    th.customerUUID = customer.uuid;
    th.targetUUID = targetUUID;
    th.taskUUID = taskUUID;
    th.targetType = targetType;
    th.type = type;
    th.targetName = targetName;
    th.createTime = new Date();
    th.save();
    return th;
  }

  public String getFriendlyDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append(type.toString(completionTime != null));
    sb.append(targetType.name());
    sb.append(" : " + targetName);
    return sb.toString();
  }

  public static CustomerTask findByTaskUUID(UUID taskUUID) {
    return find.where().eq("task_uuid", taskUUID).findUnique();
  }
}
