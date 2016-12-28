import React, { Component } from 'react';
import './App.css';

import URI from 'urijs';
import $ from 'jquery';
import {Carousel} from 'react-bootstrap';
import {Grid, Row} from 'react-bootstrap';
import {Nav, Navbar, NavDropdown, MenuItem} from 'react-bootstrap';
import ReactSwipe from 'react-swipe';
import MusicContentItem from './MusicContentItem';
import './config/globalConfigs';

let apiUrl = "";
if (window.globalConfigs.server && window.globalConfigs.apiPort) {
  apiUrl = "http://" + window.globalConfigs.server + ":" + window.globalConfigs.apiPort;
}
apiUrl += "/api/musicbot/";

class App extends Component {

  constructor() {
    super();

    this.state = {
      activeSortKey: 'recent_all',
      chatId: 0,
      music: [],
      carouselIndex: 0,
      carouselDirection: null,
      users: []
    };

    this.handleCarouselChange = this.handleCarouselChange.bind(this);
    this.handleSortSelect = this.handleSortSelect.bind(this);
  };

  getParams() {
  }

  componentDidMount() {
    let chatId = new URI().search(true).chatId;

    this.loadUsers();
    this.loadRecentMusicSets(chatId, 'P7D', null);
    this.setState({chatId: chatId});
  }

  loadUsers() {
    $.ajax({
      url: apiUrl + 'users/v1/' + this.state.chatId,
      cache: false,
      success: function(data) {
        this.setState({users: data});
      }.bind(this),
      error: function(jqxhr, status, errorThrown) {
        console.log("error!");
      }
    });
  }

  loadMusic(url) {
    $.ajax({
      url: url,
      cache: false,
      success: function(data) {
        this.setState({music: data});
      }.bind(this),
      error: function(jqxhr, status, errorThrown) {
        console.log("error!");
      }
    });
  }

  loadRecentMusicSets(chatId, duration, userId) {
    let type = 'recent';
    let url = apiUrl + 'sets/v1/' + chatId + '/' + type + '?';
    
    url += 'duration=' + duration;

    if (userId != null) {
      url += '&user=' + userId;
    }

    this.loadMusic(url);
  }

  loadPopularMusicSets(chatId, userId) {
    let type = 'popular';
    let url = apiUrl + 'sets/v1/' + chatId + '/' + type

    if (userId != null) {
      url += '?user=' + userId;
    }

    this.loadMusic(url);
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
      return (<MusicContentItem key={set.url} data={set} />);
    });
    return items;
  }

  renderUsers(source) {
    return this.state.users.map(function(user) {
      return (
        <MenuItem key={user.userId} eventKey={source + "_" + user.userId}>{user.firstName} {user.lastName}</MenuItem>
      );
    });
  }

  handleSortSelect(eventKey) {
    let type = eventKey.split("_")[0];
    let user = eventKey.split("_")[1];

    if (user === "all") {
      user = null;
    }

    if (type === "recent") {
      this.loadRecentMusicSets(this.state.chatId, "P7D", user);
    } else {
      this.loadPopularMusicSets(this.state.chatId, user);
    }

    this.setState({activeSortKey: eventKey});
  }

  render() {

    var swipeItems = this.renderSwipeItems();

    return (
      <div className="App">
      <Grid>
        <Row>
          <Navbar inverse collapseOnSelect>

            <Navbar.Header>
              <Navbar.Brand>ROOM 2</Navbar.Brand>
              <Navbar.Toggle />
            </Navbar.Header>

            <Navbar.Collapse>
              <Nav pullRight activeKey={this.state.activeSortKey} onSelect={this.handleSortSelect}>
                <NavDropdown id="sortByDropdown" title="Recent" >
                  <MenuItem eventKey="recent_all">All</MenuItem>
                  <MenuItem divider/>
                  <MenuItem header>By Person</MenuItem>
                  {this.renderUsers("recent")}
                </NavDropdown>
                <NavDropdown id="sortByDropdown" title="Popular" >
                  <MenuItem eventKey="popular_all">All</MenuItem>
                  <MenuItem divider/>
                  <MenuItem header>By Person</MenuItem>
                  {this.renderUsers("popular")}
                </NavDropdown>
              </Nav>
            </Navbar.Collapse>

          </Navbar>
        </Row>
          <ReactSwipe key={swipeItems.length} className="carousel" swipeOptions={{continuous: true}}>
            {swipeItems}
          </ReactSwipe>
        <Row>
        </Row>
      </Grid>
  

        
      </div>
    );
  }
}

export default App;
