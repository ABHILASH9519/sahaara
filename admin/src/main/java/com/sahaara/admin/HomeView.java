package com.sahaara.admin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class HomeView extends VerticalLayout {
    public HomeView() {
        add(new H1("Sahaara administration"));
        add(new Paragraph("Starter preview only. No authentication, user records, or integrations are connected."));
        add(new Paragraph("Do not expose this admin portal publicly until authentication and role-based authorization are implemented."));
    }
}
