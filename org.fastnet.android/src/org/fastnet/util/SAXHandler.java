package org.fastnet.util;

import java.lang.reflect.Field;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class SAXHandler extends DefaultHandler {

	private ItemHolder currentItem;
	private LinkedList<ItemHolder> stack = new LinkedList<ItemHolder>();
	private Object item;

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		try {
			String addition = new String(ch, start, length).trim();
			if (addition.length() > 0) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if (qName.contains(XMLBuilder.FDELIM)) {
			String[] split = qName.split(XMLBuilder.FDELIM);
			if (split[0].equals(XMLBuilder.LISTDELIM2)) {
				currentItem = getItem(split[1]);
				getFirst().list.add(currentItem.item);
			} else {
				currentItem = getItem(split[1]);
				for (Field field : getFirst().fieldList) {
					if (field.getName().equals(split[0])) {
						try {
							field.set(getFirst().item, currentItem.item);
							break;
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		} else if (qName.contains(XMLBuilder.LISTDELIM)) {
			String[] split = qName.split(XMLBuilder.LISTDELIM);
			currentItem = getItem(split[1]);
			currentItem.list = (List<?>) currentItem.item;
			for (Field field : getFirst().fieldList) {
				if (field.getName().equals(split[0])) {
					try {
						field.set(getFirst().item, currentItem.item);
						break;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			return;
		} else {
			currentItem = getItem(qName);
		}
		for (Field field : currentItem.fieldList) {
			String value = attributes.getValue(field.getName());
			if (value == null || value.length() == 0) {
				continue;
			}
			try {
				if (List.class.equals(field.getType())) {
					List<String> parseToList = LangUtils.parseToList(value, XMLBuilder.STRLISTDELIM);
					field.set(currentItem.item, parseToList);
				} else {
					field.set(currentItem.item, LangUtils.getTypeValue(value, field.getType()));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private ItemHolder getFirst() {
		ItemHolder itemHolder = null;
		if (stack.size() > 1) {
			itemHolder = stack.get(1);
		} else if (stack.size() == 1) {
			itemHolder = stack.get(0);
		}
		return itemHolder;
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	protected ItemHolder getItem(final String qName) {
		try {
			String name = qName;
			if (name.equals("com.bps.queue.common.data.EqQuestionary")) {
				name = "com.bps.queue.qb.temir.xml.EqQuestionary";
			} else if (name.equals("org.langcom.locale.LocalizedString")) {
				name = "com.bps.queue.qb.temir.xml.LocalizedString";
			}
			Class itemClass = Class.forName(name);
			Object itemIn = itemClass.newInstance();
			ArrayList fieldList = new ArrayList<Field>();
			XMLBuilder.exploreFields(fieldList, itemClass);
			ItemHolder holder = new ItemHolder(itemIn, fieldList);
			if (stack.size() == 0) {
				item = itemIn;
			}
			stack.addFirst(holder);
			return holder;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		currentItem = stack.poll();
	}

	@SuppressWarnings("unchecked")
	public <T> T getItem() {
		return (T) item;
	}
	@SuppressWarnings("rawtypes")
	private class ItemHolder {

		Object item;
		List<Field> fieldList;
		List list = new ArrayList();

		public ItemHolder(Object itemIn, List<Field> fieldList2) {
			item = itemIn;
			fieldList = fieldList2;
		}
	}
}