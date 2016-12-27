import React, { Component } from 'react';
import $ from 'jquery';
import {Carousel} from 'react-bootstrap';
import {Image} from 'react-bootstrap';
import {Nav, Navbar, NavDropdown, MenuItem} from 'react-bootstrap';
import ReactSwipe from 'react-swipe';
import './App.css';

class App extends Component {

  constructor() {
    super();

    this.state = {
      activeSortKey: 'recent',
      music: [],
      carouselIndex: 0,
      carouselDirection: null,
      users: []
    };

    this.handleCarouselChange = this.handleCarouselChange.bind(this);
    this.handleSortSelect = this.handleSortSelect.bind(this);
  };

  componentDidMount() {
    this.loadUsers();
    this.loadMusicSets();
  }

  loadUsers() {
    $.ajax({
      url: 'http://192.168.1.11:12121/api/users/v1/-141396910',
      cache: false,
      success: function(data) {
        console.log(data);
        this.setState({users: data});
      }.bind(this),
      error: function(jqxhr, status, errorThrown) {
        console.log("error!");
      }
    });
  }

  loadMusicSets() {
    $.ajax({
      url: 'http://192.168.1.11:12121/api/sets/v1/-141396910/recent',
      cache: false,
      success: function(data) {
        console.log(data);
        this.setState({music: data});
      }.bind(this),
      error: function(jqxhr, status, errorThrown) {
        console.log("error!");
      }
    });
  }

  handleCarouselChange(selectedIndex, event) {
    this.setState({carouselIndex: selectedIndex, carouselDirection: event.direction});
  }

  renderCarouselItems() {
    var items = this.state.music.map(function(set) {
      return (
        <Carousel.Item key={set.url}>
          <a href={set.url}><Image src={set.metadata.imageUrl} responsive /></a>
          <Carousel.Caption>
            <h5>{set.metadata.title}</h5>
            ({set.originalUser.firstName} - {set.originalDate})
          </Carousel.Caption>
        </Carousel.Item>
        );

    });
    return items;
  }

  renderSwipeItems() {
    var items = this.state.music.map(function(set) {
      return (
        <div key={set.url}>
          <a href={set.url}><Image src={set.metadata.imageUrl} responsive /></a>
          <h5>{set.metadata.title}</h5>
            ({set.originalUser.firstName} - {set.originalDate})
        </div>
        );
    });
    return items;
  }

  renderUsers() {
    return this.state.users.map(function(user) {
      return (
        <MenuItem key={user.userId} eventKey={user.userId}>{user.firstName} {user.lastName}</MenuItem>
      );
    });
  }

  handleSortSelect(eventKey) {
    console.log(eventKey);
    this.setState({activeSortKey: eventKey});
  }

  render() {

    var swipeItems = this.renderSwipeItems();

    return (
      <div className="App">
        <Navbar inverse collapseOnSelect>

          <Navbar.Header>
            <Navbar.Brand>ROOM 2</Navbar.Brand>
            <Navbar.Toggle />
          </Navbar.Header>

          <Navbar.Collapse>
            <Nav activeKey={this.state.activeSortKey} onSelect={this.handleSortSelect}>
              <NavDropdown id="sortByDropdown" title="Sort" >
                <MenuItem eventKey="recent">Recent</MenuItem>
                <MenuItem divider/>
                <MenuItem header>By Person</MenuItem>
                {this.renderUsers()}
              </NavDropdown>
            </Nav>
          </Navbar.Collapse>

        </Navbar>

        <ReactSwipe key={swipeItems.length} className="carousel" continuous={true}>
          {swipeItems}
        </ReactSwipe>
      </div>
    );
  }
}

export default App;
