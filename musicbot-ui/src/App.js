import React, { Component } from 'react';
import './App.css';

import URI from 'urijs';
import moment from 'moment';
import $ from 'jquery';
import {Alert} from 'react-bootstrap';
import {Button} from 'react-bootstrap';
import {Carousel} from 'react-bootstrap';
import {Grid, Row} from 'react-bootstrap';
import {Nav, Navbar, NavDropdown, MenuItem} from 'react-bootstrap';
import {Image} from 'react-bootstrap';
import ReactSwipe from 'react-swipe';
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
      userId: 0,
      music: [],
      loadedMusic: false,
      carouselIndex: 0,
      carouselDirection: null,
      users: []
    };

    this.handleCarouselChange = this.handleCarouselChange.bind(this);
    this.handleSortSelect = this.handleSortSelect.bind(this);
  };

  componentDidMount() {
    let uri = new URI().search(true);
    let chatId = uri.chatId;
    let userId = uri.userId;
    
    this.loadUsers(chatId);
    this.loadRecentMusicSets(chatId, 'P7D', null);
    this.setState({chatId: chatId, userId: userId});
  }

  playMusicSet(chatId, setId, userId) {
    let url = apiUrl + 'sets/v1/' + chatId + "/play/" + setId + "/";
    if (userId != null) {
      url += '?userId=' + userId;
    }

    $.ajax({
      url: url,
      method: 'POST',
      success: function(data) {
        console.log(data);
      }
    });
  }

  loadUsers(chatId) {
    $.ajax({
      url: apiUrl + 'users/v1/' + chatId,
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
        this.setState({music: data, loadedMusic: true});
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
      url += '&userId=' + userId;
    }

    this.loadMusic(url);
  }

  loadPopularMusicSets(chatId, userId) {
    let type = 'popular';
    let url = apiUrl + 'sets/v1/' + chatId + '/' + type;

    if (userId != null) {
      url += '?userId=' + userId;
    }

    this.loadMusic(url);
  }

  loadBrowsingMusicSets(chatId, userId) {
    let type = 'browse';
    let url = apiUrl + 'sets/v1/' + chatId + '/' + type;

    if (userId != null) {
      url += '?userId=' + userId;
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

  handleSwipeItemClick(event) {
    this.self.playMusicSet(this.self.state.chatId, this.set._id, this.self.state.userId);
    window.location = this.set.url;
  }

  renderSwipeItems() {
    var self = this;

    var items = this.state.music.map(function(set) {
      var date = moment(set.originalDate * 1000).format('MMMM Do YYYY, h:mm a');

      return (
        <div key={set.url}>
          <Button bsStyle="link" key={set.url} onClick={self.handleSwipeItemClick.bind({self: self, set: set})}>
            <Image src={set.metadata.imageUrl} responsive />
          </Button>
          <h4>{set.metadata.title}</h4>
            Originally posted by {set.originalUser.firstName} on {date}
            <br />
            {set.plays.length} play(s), {set.references.length} reference(s).
        </div>
      );
    });
    return items;
  }

  renderAlertItem(bsStyle, title, description) {
    return (
      <div key={title}>
        <Alert bsStyle={bsStyle}>
          <h4>{title}</h4>
          <p>{description}</p>
        </Alert>
      </div>
      );
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
    } else if (type === "popular") {
      this.loadPopularMusicSets(this.state.chatId, user);
    } else {
      this.loadBrowsingMusicSets(this.state.chatId, user);
    }

    this.setState({activeSortKey: eventKey});
  }

  render() {

    if (typeof this.state.chatId === 'undefined' || typeof this.state.userId === 'undefined') {
      return (
        <Alert bsStyle="danger">
          <h4>No Data Found!</h4>
          <p>Double check the url!</p>
        </Alert>
        );
    } else {
      var swipeItems = this.renderSwipeItems();

      if (swipeItems.length === 0) {
        if (this.state.loadedMusic) {
          swipeItems.push(this.renderAlertItem("warning", "Nothing found", "Try sorting another way."));  
        } else {
          swipeItems.push(<div key="loading">Loading...</div>);
        }
      }

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
                  <NavDropdown id="sortByRecentDropdown" title="Recent" >
                    <MenuItem eventKey="recent_all">All</MenuItem>
                    <MenuItem divider/>
                    <MenuItem header>By Person</MenuItem>
                    {this.renderUsers("recent")}
                  </NavDropdown>
                  <NavDropdown id="sortByPopularDropdown" title="Popular" >
                    <MenuItem eventKey="popular_all">All</MenuItem>
                    <MenuItem divider/>
                    <MenuItem header>By Person</MenuItem>
                    {this.renderUsers("popular")}
                  </NavDropdown>
                  <NavDropdown id="browseDropdown" title="Browse">
                    <MenuItem eventKey="browse_all">All</MenuItem>
                    <MenuItem divider/>
                    <MenuItem header>By Person</MenuItem>
                    {this.renderUsers("browse")}
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
}

export default App;
