/**
 * Copyright 2018-2023 the original author or Linlan authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.linlan.doc.model.framework;

public class PageableAsQueryParam {

  /**
   * Page you want to retrieve (0..N)
   */
  private Integer page;

  /**
   * Number of records per page
   */
  private Integer size;

  /**
   * Sorting criteria in the format: property(,asc|desc),Default sort order is ascending.
   * Multiple sort criteria are supported.
   */
  private String  sort;

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }
}
