package com.github.mvysny.kaributools

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._text
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import kotlin.streams.toList
import kotlin.test.expect

fun DynaNodeGroup.componentUtilsTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("removeFromParent()") {
        test("component with no parent") {
            val t = Text("foo")
            t.removeFromParent()
            expect(null) { t.parent.orElse(null) }
        }
        test("nested component") {
            val fl = FlexLayout().apply { add(Label("foo")) }
            val label = fl.getComponentAt(0)
            expect(fl) { label.parent.get() }
            label.removeFromParent()
            expect(null) { label.parent.orElse(null) }
            expect(0) { fl.componentCount }
        }
    }

    test("serverClick()") {
        val b = Button()
        var clicked = 0
        b.addClickListener { clicked++ }
        b.serverClick()
        expect(1) { clicked }
    }

    test("tooltip") {
        val b = Button()
        expect(null) { b.tooltip }
        b.tooltip = ""
        expect<String?>("") { b.tooltip } // https://youtrack.jetbrains.com/issue/KT-32501
        b.tooltip = "foo"
        expect<String?>("foo") { b.tooltip } // https://youtrack.jetbrains.com/issue/KT-32501
        b.tooltip = null
        expect(null) { b.tooltip }
    }

    test("addContextMenuListener smoke") {
        Button().addContextMenuListener({})
    }

    group("findAncestor") {
        test("null on no parent") {
            expect(null) { Button().findAncestor { false } }
        }
        test("null on no acceptance") {
            val button = Button()
            UI.getCurrent().add(button)
            expect(null) { button.findAncestor { false } }
        }
        test("finds UI") {
            val button = Button()
            UI.getCurrent().add(button)
            expect(UI.getCurrent()) { button.findAncestor { it is UI } }
        }
        test("doesn't find self") {
            val button = Button()
            UI.getCurrent().add(button)
            expect(UI.getCurrent()) { button.findAncestor { true } }
        }
    }

    group("findAncestorOrSelf") {
        test("null on no parent") {
            expect(null) { Button().findAncestorOrSelf { false } }
        }
        test("null on no acceptance") {
            val button = Button()
            UI.getCurrent().add(button)
            expect(null) { button.findAncestorOrSelf { false } }
        }
        test("finds self") {
            val button = Button()
            UI.getCurrent().add(button)
            expect(button) { button.findAncestorOrSelf { true } }
        }
    }

    test("isNestedIn") {
        expect(false) { Button().isNestedIn(UI.getCurrent()) }
        val button = Button()
        UI.getCurrent().add(button)
        expect(true) { button.isNestedIn(UI.getCurrent()) }
    }

    test("isAttached") {
        expect(true) { UI.getCurrent().isAttached() }
        expect(false) { Button("foo").isAttached() }
        expect(true) {
            val button = Button()
            UI.getCurrent().add(button)
            button.isAttached()
        }
        UI.getCurrent().close()
        expect(true) { UI.getCurrent().isAttached() }
    }

    test("insertBefore") {
        val l = HorizontalLayout()
        val first = Span("first")
        l.addComponentAsFirst(first)
        val second = Span("second")
        l.insertBefore(second, first)
        expect("second, first") { l.children.toList().map { it._text } .joinToString() }
        l.insertBefore(Span("third"), first)
        expect("second, third, first") { l.children.toList().map { it._text } .joinToString() }
    }

    test("isNotEmpty") {
        val l = HorizontalLayout()
        expect(false) { l.isNotEmpty }
        l.addComponentAsFirst(Span("first"))
        expect(true) { l.isNotEmpty }
        l.removeAll()
        expect(false) { l.isNotEmpty }
    }

    test("isEmpty") {
        val l = HorizontalLayout()
        expect(true) { l.isEmpty }
        l.addComponentAsFirst(Span("first"))
        expect(false) { l.isEmpty }
        l.removeAll()
        expect(true) { l.isEmpty }
    }

    test("addClassNames2") {
        val div = Div().apply { addClassNames2("foo  bar    baz") }
        expect(true) { div.classNames.containsAll(listOf("foo", "bar", "baz")) }
    }

    test("placeholder") {
        var c: Component = TextField().apply { placeholder = "foo" }
        expect("foo") { c.placeholder }
        c.placeholder = ""
        expect("") { c.placeholder }
        c = TextArea().apply { placeholder = "foo" }
        expect("foo") { c.placeholder }
        c.placeholder = ""
        expect("") { c.placeholder }
        c = Button() // doesn't support placeholder
        expect(null) { c.placeholder }
        expectThrows(IllegalStateException::class, "Button doesn't support setting placeholder") {
            c.placeholder = "foo"
        }
    }

    test("label") {
        var c: Component = TextField().apply { label = "foo" }
        expect("foo") { c.label }
        c.label = ""
        expect("") { c.label }
        c = Checkbox().apply { label = "foo" }
        expect("foo") { c.label }
        c.label = ""
        expect("") { c.label }
    }

    test("caption") {
        var c: Component = Button("foo")
        expect("foo") { c.caption }
        c.caption = ""
        expect("") { c.caption }
        c = Checkbox().apply { caption = "foo" }
        expect("foo") { c.caption }
        c.caption = ""
        expect("") { c.caption }
        val fl = FormLayout()
        c = fl.addFormItem(Button(), "foo")
        expect("foo") { c.caption }
    }
}
