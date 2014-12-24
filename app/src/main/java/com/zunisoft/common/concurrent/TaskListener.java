/*
 * ZUNISOFT CONFIDENTIAL
 * _____________________
 *
 *  Copyright [2014] - [2015] ZuniSoft, LLC
 *  All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of ZuniSoft, LLC and its suppliers, if any.
 * The intellectual and technical concepts contained herein
 * are proprietary to ZuniSoft, LLC and its suppliers and may
 * be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from
 * ZuniSoft, LLC.
 */

package com.zunisoft.common.concurrent;

/**
 * Task listener interface.
 *
 * @author krdavis
 */
public interface TaskListener {
    /**
     * Called when a task starts execution.
     */
    void onTaskStarted();

    /**
     * Called when a task finishes execution.
     *
     * @param result
     *            Result message for task that has finished executing.
     */
    void onTaskFinished(String result);
}
