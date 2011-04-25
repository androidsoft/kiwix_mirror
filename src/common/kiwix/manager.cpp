/*
 * Copyright 2011 Emmanuel Engelhart <kelson@kiwix.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

#include "manager.h"
#include <pugixml.hpp>

namespace kiwix {

  /* Constructor */
  Manager::Manager() {
  }
  
  /* Destructor */
  Manager::~Manager() {
  }

  bool Manager::readFile(const string path) {
    pugi::xml_document doc;
    pugi::xml_parse_result result = doc.load_file(path.c_str());

    if (result) {
      pugi::xml_node libraryNode = doc.child("library");
      library.current = libraryNode.attribute("current").value();

      for (pugi::xml_node bookNode = libraryNode.child("book"); bookNode; bookNode = bookNode.next_sibling("book")) {
	kiwix::Book book;
	book.id = bookNode.attribute("id").value();
	book.path = bookNode.attribute("path").value();
	book.last = bookNode.attribute("last").value();
	book.indexPath = bookNode.attribute("indexPath").value();
	book.indexType = bookNode.attribute("indexType").value() == "xapian" ? XAPIAN: CLUCENE;
	book.title = bookNode.attribute("title").value();
	book.description = bookNode.attribute("description").value();
	book.language = bookNode.attribute("language").value();
	book.date = bookNode.attribute("date").value();
	book.creator = bookNode.attribute("creator").value();
	book.url = bookNode.attribute("url").value();
	book.articleCount = bookNode.attribute("articleCount").value();
	book.mediaCount = bookNode.attribute("mediaCount").value();
	library.addBook(book);
      }

    }

    return result;
  }

  bool Manager::writeFile(const string path) {
    pugi::xml_document doc;

    /* Add the library node */
    pugi::xml_node libraryNode = doc.append_child("library");

    if (library.current != "")
      libraryNode.append_attribute("current") = library.current.c_str();
    
    /* Add each book */
    std::vector<kiwix::Book>::iterator itr;
    for ( itr = library.books.begin(); itr != library.books.end(); ++itr ) {
      pugi::xml_node bookNode = libraryNode.append_child("book");
      bookNode.append_attribute("id") = itr->id.c_str();
      bookNode.append_attribute("path") = itr->path.c_str();

      if (itr->last != "")
	bookNode.append_attribute("last") = itr->last.c_str();

      if (itr->indexPath != "") {
	bookNode.append_attribute("indexPath") = itr->indexPath.c_str();
	if (itr->indexType == XAPIAN)
	  bookNode.append_attribute("indexType") = "xapian";
	else
	  bookNode.append_attribute("indexType") = "clucene";
      }

      if (itr->title != "")
	bookNode.append_attribute("title") = itr->title.c_str();

      if (itr->description != "")
	bookNode.append_attribute("description") = itr->description.c_str();

      if (itr->language != "")
	bookNode.append_attribute("language") = itr->language.c_str();

      if (itr->date != "")
	bookNode.append_attribute("date") = itr->date.c_str();

      if (itr->creator != "")
	bookNode.append_attribute("creator") = itr->creator.c_str();

      if (itr->url != "")
	bookNode.append_attribute("url") = itr->url.c_str();

      if (itr->articleCount != "")
	bookNode.append_attribute("articleCount") = itr->articleCount.c_str();

      if (itr->mediaCount != "")
	bookNode.append_attribute("mediaCount") = itr->mediaCount.c_str();

    }

    /* saving file */
    doc.save_file(path.c_str());

    return true;
  }

  bool Manager::addBookFromPath(const string path, const string url) {
    kiwix::Book book;
    
    /* Open the ZIM file */
    kiwix::Reader reader = kiwix::Reader(path);
    book.path = path;
    book.id = reader.getId();
    book.title = reader.getTitle();
    book.description = reader.getDescription();
    book.language = reader.getLanguage();
    book.date = reader.getDate();
    book.creator = reader.getCreator();
    book.url = url;
   
    std::ostringstream articleCountStream;
    articleCountStream << reader.getArticleCount();
    book.articleCount = articleCountStream.str();

    std::ostringstream mediaCountStream;
    mediaCountStream << reader.getMediaCount();
    book.mediaCount = mediaCountStream.str();

    library.addBook(book);

    return true;
  }

  bool Manager::removeBookByIndex(const unsigned int bookIndex) {
    return this->library.removeBookByIndex(bookIndex);
  }

  kiwix::Library Manager::cloneLibrary() {
    return this->library;
  }

}